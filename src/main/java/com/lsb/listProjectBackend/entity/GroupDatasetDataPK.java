package com.lsb.listProjectBackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupDatasetDataPK implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String groupName;
    private String primeValue;
}
