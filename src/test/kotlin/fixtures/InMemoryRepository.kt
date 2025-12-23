package fixtures

import arrow.core.Either
import org.example.domain.error.Error
import org.example.domain.error.NotFoundError
import org.example.repository.Repository

class InMemoryRepository<T> : Repository<T> {
    private var data: T? = null

    override fun load(): Either<Error, T> {
        return if (data != null) {
            Either.Right(data!!)
        } else {
            Either.Left(NotFoundError)
        }
    }

    override fun save(data: T): Either<Error, Unit> {
        this.data = data
        return Either.Right(Unit)
    }
}