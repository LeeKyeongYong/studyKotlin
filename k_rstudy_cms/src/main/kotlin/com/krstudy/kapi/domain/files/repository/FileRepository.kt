package com.krstudy.kapi.domain.files.repository

import com.krstudy.kapi.domain.files.entity.FileEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FileRepository : JpaRepository<FileEntity, Long>