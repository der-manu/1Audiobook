package voice.data

import androidx.room.migration.Migration
import com.squareup.anvil.annotations.MergeComponent
import voice.common.AppScope
import javax.inject.Singleton

@Singleton
@MergeComponent(
  scope = AppScope::class,
)
interface TestComponent {

  val migrations: Set<@JvmSuppressWildcards Migration>
}

internal fun allMigrations(): Array<Migration> {
  return DaggerTestComponent.create()
    .migrations.toTypedArray()
}
