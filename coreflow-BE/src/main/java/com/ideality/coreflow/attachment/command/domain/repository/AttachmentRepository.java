package com.ideality.coreflow.attachment.command.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ideality.coreflow.attachment.command.domain.aggregate.Attachment;
import com.ideality.coreflow.attachment.command.domain.aggregate.FileTargetType;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
	Optional<Attachment> findByTargetIdAndTargetType(Long targetId, FileTargetType targetType);

	List<Attachment> findListByTargetIdAndTargetType(Long targetId, FileTargetType targetType);

	Boolean existsAttachmentByTargetId(Long templateId);
}
