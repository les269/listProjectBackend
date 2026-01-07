package com.lsb.listProjectBackend.repository;

import com.lsb.listProjectBackend.entity.Setting;
import com.lsb.listProjectBackend.entity.ShareTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareTagRepository extends JpaRepository<ShareTag, String> {
}
