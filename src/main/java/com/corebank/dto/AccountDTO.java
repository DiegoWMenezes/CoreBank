package com.corebank.dto;

import jakarta.validation.constraints.NotBlank;

public class AccountDTO {

    @NotBlank(message = "Nome do titular é obrigatório")
    private String ownerName;

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
}