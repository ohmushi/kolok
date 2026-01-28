package cat.ohmushi.kolok.planning.domain.events

fun interface EventHandler<E: DomainEvent> {
    suspend fun handle(event: E)
}