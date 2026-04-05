import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals

class MainInputEncodingTest {
    @Test
    fun `redirected stdin uses utf8`() {
        assertEquals(
            StandardCharsets.UTF_8,
            detectConsoleCharset(
                hasConsole = false,
                nativeEncoding = "Cp1251"
            )
        )
    }

    @Test
    fun `interactive console keeps native encoding`() {
        assertEquals(
            Charset.forName("Cp1251"),
            detectConsoleCharset(
                hasConsole = true,
                nativeEncoding = "Cp1251"
            )
        )
    }

    @Test
    fun `sanitize removes unicode bom`() {
        assertEquals("use huggingface", sanitizeConsoleInput("\uFEFFuse huggingface"))
    }

    @Test
    fun `sanitize removes broken bom marker`() {
        assertEquals("use huggingface", sanitizeConsoleInput("п»їuse huggingface"))
    }

    @Test
    fun `sanitize removes latin bom marker`() {
        assertEquals("use huggingface", sanitizeConsoleInput("ï»¿use huggingface"))
    }
}
