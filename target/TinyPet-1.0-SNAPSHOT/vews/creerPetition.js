let Petition = {
    title: '',
    description: '',
    nbSignatures: 0,
    autorId: '',
    autorName: '',
    userToken: '',

    createPetition: function (petition) {
        if(!Petition.autorId){
            return;
        }    
        return m.request({
            method: "POST",
            url: "/_ah/api/myApi/v1/postPetition",
            body: {
                userToken: petition.userToken,
                petition: {
                    autorId: petition.autorId,
                    title: petition.title,
                    description: petition.description
                }
            }
        }).then(function (result) {
            Petition.title = '';
            Petition.description = '';
            console.log(result)
            PetitionList.loadPetitions();
        });
    },
    signPetition: function (petId) {    
        if(!Petition.autorId){
            alert("Please sign in before signing the petittion")
            return;
        } 
        console.log(petId, Petition.userId)
        return m.request({
            method: "POST",
            url: "/_ah/api/myApi/v1/signature",
            body: {
                idPetition: petId,  // Changed from petitionId to petId
                idUser: Petition.userId,
                name: Petition.name
            }
        }).then(function (result) {
            console.log(result);
            PetitionList.loadPetitions();
        });
    }
}

let PetitionForm = {
    petition: Petition,
    alertAuthentication: false,
    showNextButton: false,
    currentStep: 0,
    steps: [
        {
            title: "Créer votre pétition",
            content: m("div", [
                m("button.button.is-primary.is-large", { 
                    onclick: function() { 
                        if (!PetitionForm.petition.autorId) {
                            PetitionForm.alertAuthentication = true;
                        } else {
                            PetitionForm.alertAuthentication = false;
                            PetitionForm.showNextButton = true; 
                            PetitionForm.currentStep++;
                        } 
                    } 
                }, "Lancer la création d'une pétition")
            ])
        },
        {
            title: "Titre de la pétition",
            content: m("input.input.input-short", { type: "text", placeholder: "Titre", oninput: function(e) { PetitionForm.petition.title = e.target.value; } }) // Use PetitionForm.petition.title
        },
        {
            title: "Sujet de la pétition",
            content: m("textarea.textarea.textarea-short", { placeholder: "Description", oninput: function(e) { PetitionForm.petition.description = e.target.value; } }) // Use PetitionForm.petition.description
        }
    ],
    view: function() {
        return m("div.petition-form", [
            PetitionForm.steps.map(function(step, index) {
                return m("div", { class: "form-step " + (PetitionForm.currentStep === index ? "active" : "") }, [
                    m("h2", { class: "title is-size-3" }, step.title),
                    step.content
                ]);
            }),
            PetitionForm.showNextButton ? m("div.form-navigation", [
                PetitionForm.currentStep > 0 ? m("button.button", { onclick: function() { PetitionForm.currentStep--; } }, "Précédent") : null,
                PetitionForm.currentStep < PetitionForm.steps.length - 1 ? m("button.button.is-primary", { onclick: function() { PetitionForm.currentStep++; } }, "Suivant") : m("button.button.is-success", { onclick: function() { console.log(PetitionForm.petition); Petition.createPetition(PetitionForm.petition); PetitionForm.resetForm(); } }, "Créer la pétition")
            ]) : null,
            PetitionForm.alertAuthentication ? m("div.notification.is-danger", { style: { marginTop: "10px" } }, [
                    m("button.delete", { onclick: function() { PetitionForm.alertAuthentication = false; } }),
                    m("span", "Veuillez vous connecter")
                ])
                : null
        ]);
    },
    resetForm: function() {
        PetitionForm.currentStep = 0;
        PetitionForm.petition.title = "";
        PetitionForm.petition.description = "";
        PetitionForm.showNextButton = false;
        PetitionForm.alertAuthentication = false;
    }
};