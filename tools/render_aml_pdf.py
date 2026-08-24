from pathlib import Path
import re


SOURCE = Path(__file__).parents[1] / "docs" / "AML_PROJECT_EXPLAINER.md"
OUTPUT = Path(__file__).parents[1] / "docs" / "AML_PROJECT_EXPLAINER.pdf"
PAGE_WIDTH = 612
PAGE_HEIGHT = 792
LEFT = 54
TOP = 738
BOTTOM = 54
BODY_SIZE = 9
LINE_HEIGHT = 13


def pdf_escape(value):
    return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")


def wrap_text(value, width):
    words = value.split()
    lines = []
    current = ""
    for word in words:
        candidate = word if not current else current + " " + word
        if len(candidate) <= width:
            current = candidate
        else:
            if current:
                lines.append(current)
            current = word
    if current:
        lines.append(current)
    return lines or [""]


def markdown_lines(markdown):
    output_lines = []
    for raw_line in markdown.splitlines():
        line = raw_line.rstrip()
        if line.startswith("# "):
            output_lines.append(("title", line[2:]))
        elif line.startswith("## "):
            output_lines.extend([("space", ""), ("heading", line[3:])])
        elif line.startswith("### "):
            output_lines.append(("subheading", line[4:]))
        elif line.startswith("- "):
            output_lines.append(("body", "- " + line[2:]))
        elif re.match(r"^\d+\. ", line):
            output_lines.append(("body", line))
        elif line:
            output_lines.append(("code" if line.startswith("    ") else "body", line.strip()))
        else:
            output_lines.append(("space", ""))
    return output_lines


def content_stream(page_lines, page_number):
    commands = ["BT"]
    for style, line in page_lines:
        if style == "title":
            size, font, color = 22, "F2", "0.08 0.22 0.30"
        elif style == "heading":
            size, font, color = 13, "F2", "0.00 0.40 0.45"
        elif style == "subheading":
            size, font, color = 10, "F2", "0.10 0.25 0.30"
        elif style == "code":
            size, font, color = 8, "F3", "0.20 0.20 0.20"
        else:
            size, font, color = BODY_SIZE, "F1", "0.14 0.16 0.18"
        commands.append(f"/{font} {size} Tf {color} rg")
        commands.append(f"1 0 0 1 {LEFT} {page_number[1]} Tm")
        commands.append(f"({pdf_escape(line)}) Tj")
        page_number[1] -= LINE_HEIGHT if style != "space" else LINE_HEIGHT // 2
    commands.append("ET")
    return "\n".join(commands).encode("latin-1", "replace")


def build_pdf(lines):
    usable_lines = []
    for style, value in lines:
        if style in {"title", "heading", "subheading", "space"}:
            usable_lines.append((style, value))
        else:
            for wrapped in wrap_text(value, 88):
                usable_lines.append((style, wrapped))

    pages = []
    current_page = []
    current_height = TOP
    for item in usable_lines:
        item_height = LINE_HEIGHT if item[0] != "space" else LINE_HEIGHT // 2
        if current_page and current_height - item_height < BOTTOM:
            pages.append(current_page)
            current_page = []
            current_height = TOP
        current_page.append(item)
        current_height -= item_height
    if current_page:
        pages.append(current_page)

    objects = [b"<< /Type /Catalog /Pages 2 0 R >>", None]
    page_ids = []
    content_ids = []
    for page_index, page in enumerate(pages, start=1):
        page_id = len(objects) + 1
        objects.append(None)
        content_id = len(objects) + 1
        stream = content_stream(page, [page_index, TOP])
        objects.append(b"<< /Length " + str(len(stream)).encode() + b" >>\nstream\n" + stream + b"\nendstream")
        page_ids.append(page_id)
        content_ids.append(content_id)
    objects[1] = (b"<< /Type /Pages /Kids [" + b" ".join(str(page_id).encode() + b" 0 R" for page_id in page_ids)
                  + b"] /Count " + str(len(page_ids)).encode() + b" >>")
    font_ids = [len(objects) + 1, len(objects) + 2, len(objects) + 3]
    objects.extend([
        b"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
        b"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>",
        b"<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>",
    ])
    for page_id, content_id in zip(page_ids, content_ids):
        objects[page_id - 1] = (b"<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] "
            + b"/Resources << /Font << /F1 " + str(font_ids[0]).encode() + b" 0 R /F2 "
            + str(font_ids[1]).encode() + b" 0 R /F3 " + str(font_ids[2]).encode() + b" 0 R >> >> /Contents "
            + str(content_id).encode() + b" 0 R >>")

    output = bytearray(b"%PDF-1.4\n%\xe2\xe3\xcf\xd3\n")
    offsets = [0]
    for object_id, data in enumerate(objects, start=1):
        offsets.append(len(output))
        output.extend(f"{object_id} 0 obj\n".encode())
        output.extend(data)
        output.extend(b"\nendobj\n")
    xref_position = len(output)
    output.extend(f"xref\n0 {len(objects) + 1}\n".encode())
    output.extend(b"0000000000 65535 f \n")
    for offset in offsets[1:]:
        output.extend(f"{offset:010d} 00000 n \n".encode())
    output.extend((f"trailer\n<< /Size {len(objects) + 1} /Root 1 0 R >>\n"
                   f"startxref\n{xref_position}\n%%EOF\n").encode())
    return bytes(output)


if __name__ == "__main__":
    OUTPUT.write_bytes(build_pdf(markdown_lines(SOURCE.read_text(encoding="utf-8"))))
    print(f"Created {OUTPUT} ({OUTPUT.stat().st_size} bytes)")