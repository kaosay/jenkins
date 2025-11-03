pipelineJob("test-schdule-service") {
  description("this is my first job")
  keepDependencies(false)
  parameters {
    choiceParam("test", [1, 2, 3], "")
  }
  definition {
    cpsScm {
      scm {
        git {
          remote {
            github("https://gitlab.com/xxx/xxx.git", "https")
            credentials("24982560-17fc-4589-819b-bc5bea89da77")
          }
          branch("*/master")
        }
      }
      scriptPath("Jenkinsfile")
    }
  }
  disabled(false)
}
