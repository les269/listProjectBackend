package com.lsb.listProjectBackend.service.impl;

import com.lsb.listProjectBackend.domain.DiskTO;
import com.lsb.listProjectBackend.entity.local.Disk;
import com.lsb.listProjectBackend.mapper.DiskMapper;
import com.lsb.listProjectBackend.repository.local.DiskRepository;
import com.lsb.listProjectBackend.service.DiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiskServiceImpl implements DiskService {

    @Autowired
    private DiskRepository diskRepository;

    private final DiskMapper diskMapper = DiskMapper.INSTANCE;

    @Override
    public List<DiskTO> getAll() {
        return diskMapper.toDomainList(diskRepository.findAll());
    }

    @Override
    public void add(String disk) {
        if (disk == null || disk.trim().isEmpty())
            return;
        String key = disk.trim();
        if (diskRepository.existsById(key))
            return;

        Disk entity = new Disk();
        entity.setDisk(key);

        File f = new File(key + ":" + File.separator);
        if (f.exists() && f.canRead()) {
            entity.setTotalSpace(f.getTotalSpace());
            entity.setFreeSpace(f.getFreeSpace());
            entity.setUsableSpace(f.getUsableSpace());
        } else {
            entity.setTotalSpace(0L);
            entity.setFreeSpace(0L);
            entity.setUsableSpace(0L);
        }
        entity.setUpdateDate(LocalDateTime.now());
        diskRepository.save(entity);
    }

    @Override
    public void refresh() {
        List<Disk> all = diskRepository.findAll();
        List<Disk> updated = all.stream().peek(d -> {
            File f = new File(d.getDisk() + ":" + File.separator);
            if (f.exists() && f.canRead()) {
                d.setTotalSpace(f.getTotalSpace());
                d.setFreeSpace(f.getFreeSpace());
                d.setUsableSpace(f.getUsableSpace());
            }
        }).toList();

        diskRepository.saveAll(updated);
    }

    @Override
    public void delete(String disk) {
        if (disk == null || disk.trim().isEmpty()) return;
        String key = disk.trim();
        if (diskRepository.existsById(key)) {
            diskRepository.deleteById(key);
        }
    }
}
