package cat.ohmushi.kolok.planning.adapters.`in`

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/health"], produces = [MediaType.APPLICATION_JSON_VALUE])
class HealthController() {

    @GetMapping
    fun health(): ResponseEntity<String> {
        return ResponseEntity.ok("ok")
    }
}