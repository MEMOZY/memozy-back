package com.momozy.memozy_back.test;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // 이메일로 찾기 같은 쿼리도 여기에 추가 가능
}