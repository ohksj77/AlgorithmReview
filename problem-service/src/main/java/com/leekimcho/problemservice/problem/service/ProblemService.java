package com.leekimcho.problemservice.problem.service;

import com.leekimcho.problemservice.common.advice.exception.EntityNotFoundException;
import com.leekimcho.problemservice.common.dto.MemberDto;
import com.leekimcho.problemservice.problem.dto.request.ProblemRequestDto;
import com.leekimcho.problemservice.problem.entity.*;
import com.leekimcho.problemservice.problem.repository.ProblemQueryRepository;
import com.leekimcho.problemservice.problem.repository.ProblemRepository;
import com.leekimcho.problemservice.problem.repository.TagRepository;
import com.leekimcho.problemservice.review.entity.Review;
import com.leekimcho.problemservice.review.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final TagRepository tagRepository;
    private final ProblemQueryRepository problemQueryRepository;

    private final ReviewMapper reviewMapper;

    @Transactional(readOnly = true)
    public List<Problem> getProblemListByStepOrTag(LocalDateTime modifiedDate, Long cursorId, int step, String tagName, Pageable page) {
        if (step == 0 && tagName.isBlank()) {
            return getProblemList(modifiedDate, cursorId, page);
        }
        else if (step == 0) {
            return getProblemListByTagName(modifiedDate, tagName, cursorId, page);
        }
        else {
            return getProblemList(modifiedDate, cursorId, page);
        }
    }

    @Transactional(readOnly = true)
    public Problem getProblemById(Long problemId) {
        return problemRepository.findById(problemId).orElseThrow(EntityNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public long getProblemCount() {
        return problemRepository.count();
    }

    @Transactional
    public Long registerProblem(Problem problem, ProblemRequestDto registerDto) {
        Review review = reviewMapper.toEntity(problem, registerDto);

        List<ProblemTag> tagList = registerDto.getTagList().stream()
                .map(tagName -> tagRepository.findByTagName(tagName)
                        .map(
                                tag -> new ProblemTag(problem, tag)).orElseGet(
                                () -> new ProblemTag(problem, new Tag(tagName))
                        )).collect(toList());

        problem.setReviewAndTagList(review, tagList);
        problemRepository.save(problem);

        return problem.getId();
    }

    @Transactional
    public void updateStep(Long problemId, MemberDto member, int step) {
        Problem updateProblem = checkValidUser(problemId, member);
        updateProblem.updateStep(step);
        problemRepository.save(updateProblem);
    }

    @Transactional
    public void updateNotificationDate(Long problemId, MemberDto member,LocalDate notificationDate) {
        Problem updateProblem = checkValidUser(problemId, member);
        updateProblem.updateNotificationDate(notificationDate);
        problemRepository.save(updateProblem);
    }

    @Transactional
    public void deleteProblem(Long problemId, MemberDto member) {
        checkValidUser(problemId, member);
        problemRepository.deleteById(problemId);
    }

    public List<Problem> getProblemList(LocalDateTime modifiedDate, Long cursorId, Pageable page) {
        if (modifiedDate == null) {
            return problemRepository.findByOrderByModifiedDateDesc(page);
        }
        return problemRepository.findByModifiedDateBeforeAndIdNotOrderByModifiedDateDesc(modifiedDate, cursorId, page);
    }

    public List<Problem> getProblemListByStep(int step, Long cursorId, Pageable page) {
        return cursorId.equals(0L) ?
                problemRepository.findAllByStepOrderByModifiedDateDesc(step, page) :
                problemRepository.findByIdLessThanAndStepOrderByIdDesc(cursorId, step, page); // 커서기반 페이징
    }

    public List<Problem> getProblemListByTagName(LocalDateTime modifiedDate, String tagName, Long cursorId, Pageable page) {
        if (tagName == null) {
            return getProblemList(modifiedDate, cursorId, page);
        }
        return problemQueryRepository.findAllByTag(modifiedDate, cursorId, tagName, page.getPageSize());
    }

    private Problem checkValidUser(Long problemId, MemberDto member) {
        return problemRepository.findProblemByIdAndMemberId(problemId, member.getId()).orElseThrow(EntityNotFoundException::new);
    }
}