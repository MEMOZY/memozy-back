package com.memozy.memozy_back.domain.user.repository;

import com.memozy.memozy_back.domain.user.domain.UserPolicyAgreement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserPolicyAgreementRepository extends JpaRepository<UserPolicyAgreement, Long> {

    @Query("select "
            + "case when count(u) > 0 "
            + "then true "
            + "else false "
            + "end "
            + "from UserPolicyAgreement u "
            + "where u.id = :id")
    boolean existsById(Long id);

    List<UserPolicyAgreement> findAllByUserId(long userId);

    void deleteAllByUserId(long userId);
}