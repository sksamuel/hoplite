import com.sksamuel.hoplite.ConfigCursor
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.readers.Reader
import java.time.LocalDate

class LocalDateTime : Reader<LocalDate> {
  override fun read(cursor: ConfigCursor): ConfigResult<LocalDate> {
    cursor.asString()
    TODO()
  }
}