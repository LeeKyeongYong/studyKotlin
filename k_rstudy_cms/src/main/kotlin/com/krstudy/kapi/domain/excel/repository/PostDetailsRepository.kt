package com.krstudy.kapi.domain.excel.repository

import com.krstudy.kapi.domain.excel.entity.PostDetails
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PostDetailsRepository : JpaRepository<PostDetails, Long> {}