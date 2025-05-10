package unit.http.auth

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.example.http.auth.youtubeAuthRequest

class YoutubeAuthRequestTest : DescribeSpec({
    it("should return an encoded URL") {
        youtubeAuthRequest("client-id") shouldBe "https://accounts.google.com/o/oauth2/auth?client_id=client-id&redirect_uri=urn%3Aietf%3Awg%3Aoauth%3A2.0%3Aoob&response_type=code&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fyoutube.readonly&access_type=offline&prompt=consent"
    }
})