package com.aml.common.dto.request;
public class UpdateInvestigationCaseRequest {
    private String owner, status, resolution;
    public String getOwner(){return owner;} public void setOwner(String v){owner=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getResolution(){return resolution;} public void setResolution(String v){resolution=v;}
}
