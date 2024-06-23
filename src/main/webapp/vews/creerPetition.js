function getUserInfo() {
    const userJson = localStorage.getItem('user');
    if (userJson) {
        return JSON.parse(userJson);
    }
    return null;
}

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
            url: "/postPetition",
            body: {
                userToken: petition.userToken,
                petition: {
                    autorId: petition.autorId,
                    autorName: petition.autorName,
                    title: petition.title,
                    description: petition.description
                }
            }
        }).then(function (result) {
            PetitionList.loadPetitions();
            Petition.title = '';
            Petition.description = '';
        });
    },
    signPetition: function(petId) {
        const userInfo = getUserInfo();
        console.log(petId);
        console.log(userInfo.userId);
        if (!userInfo || !userInfo.userId) {
            PetitionForm.alertAuthentication = true;
            return;
        }
        m.request({
            method: "POST",
            url: "/signature",
            withCredentials: true,
            headers: {
                'Content-Type': 'application/json'
            },
            body: {
                petitionId: petId,
                userId: userInfo.userId
            }
        })
        .then(function(result) {
            console.log("Petition signed successfully:", result);
            // Reload petitions if necessary
            PetitionList.loadPetitions();
        })
        .catch(function(error) {
            console.error("Failed to sign petition:", error);
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
                m("button.button.is-primary", { 
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
                PetitionForm.currentStep < PetitionForm.steps.length - 1 ? m("button.button.is-primary", { onclick: function() { PetitionForm.currentStep++; } }, "Suivant") : m("button.button.is-success", { onclick: function() { Petition.createPetition(PetitionForm.petition); PetitionForm.resetForm(); } }, "Créer la pétition")
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