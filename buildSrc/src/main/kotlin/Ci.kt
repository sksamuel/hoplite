object Ci {

  // this is the version used for building snapshots
  // .GITHUB_RUN_NUMBER-snapshot will be appended
  private const val SNAPSHOT_BASE = "3.0.0"

  private val githubRunNumber = System.getenv("GITHUB_RUN_NUMBER")

  private val snapshotVersion = when (githubRunNumber) {
    null -> "$SNAPSHOT_BASE-LOCAL"
    else -> "$SNAPSHOT_BASE.${githubRunNumber}-SNAPSHOT"
  }

  private val releaseVersion = System.getenv("RELEASE_VERSION")

  val isRelease = releaseVersion != null
  val version = releaseVersion ?: snapshotVersion
}
