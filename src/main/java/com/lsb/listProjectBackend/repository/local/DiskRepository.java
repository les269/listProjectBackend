package com.lsb.listProjectBackend.repository.local;

import org.springframework.data.jpa.repository.JpaRepository;
import com.lsb.listProjectBackend.entity.local.Disk;

public interface DiskRepository extends JpaRepository<Disk, String> {

}
