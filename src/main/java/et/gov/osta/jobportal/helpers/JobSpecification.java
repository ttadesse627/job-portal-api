package et.gov.osta.jobportal.helpers;

import et.gov.osta.jobportal.domain.entities.Job;
import et.gov.osta.jobportal.domain.enums.JobStatus;
import et.gov.osta.jobportal.domain.enums.JobType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JobSpecification {

    public static Specification<Job> filter(
            String title,
            String position,
            String location,
            JobType jobType,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (title != null && !title.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }

            if (position != null && !position.isBlank()) {
                predicates.add(cb.equal(root.get("position"), position));
            }

            if (location != null && !location.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
            }

            if (jobType != null) {
                predicates.add(cb.equal(root.get("jobType"), jobType));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("postedDate"), startDate.atStartOfDay()));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("postedDate"), endDate.atTime(23, 59)));
            }

            predicates.add(cb.equal(root.get("status"), JobStatus.ACTIVE));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
