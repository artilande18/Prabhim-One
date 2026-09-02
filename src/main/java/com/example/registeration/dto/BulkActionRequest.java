package com.example.registeration.dto;

import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.NotEmpty;

public class BulkActionRequest {

    @NotEmpty(message = "List of IDs cannot be empty")
    private List<UUID> ids;

    public List<UUID> getIds() {
        return ids;
    }

    public void setIds(List<UUID> ids) {
        this.ids = ids;
    }
}
