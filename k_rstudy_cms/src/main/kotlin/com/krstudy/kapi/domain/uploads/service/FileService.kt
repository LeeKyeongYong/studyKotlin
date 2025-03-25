import com.krstudy.kapi.domain.uploads.entity.FileEntity
import org.springframework.web.multipart.MultipartFile

interface FileService {
    fun uploadFiles(files: Array<MultipartFile>, userId: String): List<FileEntity>
    fun getUserFiles(userId: String): List<FileEntity>
    fun deleteFile(fileId: Long, userId: String)
    fun getFileById(id: Long): FileEntity  // 새로 추가된 메서드
}
