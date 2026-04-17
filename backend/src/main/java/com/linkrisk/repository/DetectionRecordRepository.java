package com.linkrisk.repository;

import com.linkrisk.entity.DetectionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetectionRecordRepository extends JpaRepository<DetectionRecord, Long> {
}
