package com.ideality.coreflow.comment.command.application.service.impl;

import com.ideality.coreflow.comment.command.application.dto.RequestCommentDTO;
import com.ideality.coreflow.comment.command.application.dto.RequestModifyCommentDTO;
import com.ideality.coreflow.comment.command.application.service.CommentService;
import com.ideality.coreflow.comment.command.domain.aggregate.Comment;
import com.ideality.coreflow.comment.command.domain.aggregate.CommentType;
import com.ideality.coreflow.comment.command.domain.repository.CommentRepository;
import com.ideality.coreflow.common.exception.BaseException;
import com.ideality.coreflow.common.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.ideality.coreflow.common.exception.ErrorCode.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public Long createComment(RequestCommentDTO commentDTO, Long taskId, Long userId) {

        /* 설명. 잘못된 대댓글 접근에 대한 예외처리 */
        if (commentDTO.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(commentDTO.getParentCommentId())
                    .orElseThrow(() -> new BaseException(COMMENT_NOT_FOUND));

            if (parentComment.isDeleted()) {
                throw new BaseException(COMMENT_ALREADY_DELETED);
            }
        }

        Comment newComment =
                Comment.builder()
                        .content(commentDTO.getContent())
                        .isDeleted(false)
                        .isNotice(commentDTO.getIsNotice())
                        .type(commentDTO.getIsNotice() ? CommentType.NOTICE : CommentType.COMMENT)
                        .createdAt(LocalDateTime.now())
                        .userId(userId)
                        .workId(taskId)
                        .parentCommentId(commentDTO.getIsNotice() ? null : commentDTO.getParentCommentId())
                        .build();
        commentRepository.save(newComment);
        return newComment.getId();
    }

    @Override
    @Transactional
    public Long updateByDelete(Long userId, Comment comment) {

        /* 설명. 이미 삭제되었다는 예외는 도메인 내부 로직에서 */
        if (comment.isDeleted()) {
            throw new BaseException(COMMENT_ALREADY_DELETED);
        }

        comment.updateDeleted();
        commentRepository.save(comment);
        return comment.getId();
    }

    @Override
    @Transactional
    public Long updateComment(RequestModifyCommentDTO reqModify, Long userId, Comment comment) {

        if(comment.isDeleted()) {
            throw new BaseException(COMMENT_ALREADY_DELETED);
        }

        comment.updateComment(reqModify.getContent(), reqModify.getIsNotice());
        commentRepository.save(comment);
        return comment.getId();
    }

    @Override
    @Transactional
    public Long updateByNotice(Long userId, Comment comment) {

        if(comment.isDeleted()) {
            throw new BaseException(COMMENT_ALREADY_DELETED);
        }

        if(comment.isNotice() && comment.getType() == CommentType.NOTICE) {
            throw new BaseException(COMMENT_ALREADY_NOTICE);
        }

        comment.updateNotice();
        commentRepository.save(comment);
        return comment.getId();
    }

    @Override
    public Comment findById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new BaseException(COMMENT_NOT_FOUND));
    }
}
