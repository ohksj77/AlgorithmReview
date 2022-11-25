package com.leekimcho.problemservice.problem.entity;

import com.leekimcho.problemservice.common.BaseEntity;
import com.leekimcho.problemservice.review.entity.Review;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Builder
public class Problem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "problem_id", updatable = false)
    private Long id;

    @Column(updatable = false)
    private Long memberId;

    private String title;

    private String link;

    private int step;

    private LocalDate notificationDate;

    @Builder.Default
    @OrderBy("createdDate desc")
    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Review> reviewList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "problem", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ProblemTag> tagList = new ArrayList<>();

    public void setReviewAndTagList(Review review, List<ProblemTag> tagList) {
        this.reviewList = singletonList(review);
        this.tagList = tagList;
    }

    public void updateStep(int step) {
        this.step = step;
    }

    public void updateNotificationDate(LocalDate notificationDate) {
        this.notificationDate = notificationDate;
    }

}