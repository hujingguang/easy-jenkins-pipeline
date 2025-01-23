def call(String auditUsers){
                    try{
                       def userInput = input(
                          id: "inputId",
                          message: 'Do you approve the deployment?',
                          parameters: [],
                          ok: "同意",
                          submitter: auditUsers
                          )

                     }catch(Exception e){
                         currentBuild.result = 'FAILURE'
                         error "Exited"
                 }
}