package com.aml.common.dto.request;
public class UpdateInvestigationCaseRequest {
    private String owner, status, resolution, expertId, notes;
    public String getOwner(){return owner;} public void setOwner(String v){owner=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getResolution(){return resolution;} public void setResolution(String v){resolution=v;}
    public String getExpertId(){return expertId;} public void setExpertId(String v){expertId=v;}
    public String getNotes(){return notes;} public void setNotes(String v){notes=v;}
}
