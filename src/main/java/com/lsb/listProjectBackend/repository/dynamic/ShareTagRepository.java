package com.lsb.listProjectBackend.repository.dynamic;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lsb.listProjectBackend.entity.dynamic.ShareTag;

public interface ShareTagRepository extends JpaRepository<ShareTag, String> {
}
