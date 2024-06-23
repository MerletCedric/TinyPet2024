function getUserInfo() {
    const userJson = localStorage.getItem('user');
    if (userJson) {
        return JSON.parse(userJson);
    }
    return null;
}

let PetitionList = {
    petitions: [],
    selection: 'topHundred',
    loadPetitions: function() {
        const userInfo = getUserInfo();
        m.request({
            method: "GET",
            url: "/getPetitions",
            withCredentials: true,
            params: {
                selection: PetitionList.selection,
                userId: userInfo.userId ? userInfo.userId : ''
            }
        })
        .then(function(result) {
            if (result && result.length > 0) {
                PetitionList.petitions = result;
            } else {
                PetitionList.petitions = [];
            }
            console.log(PetitionList.petitions);
            m.redraw();
        })
        .catch(function(error) {
            console.error("Failed to load petitions:", error);
            PetitionList.petitions = [];
            m.redraw();
        });
    },
    formatDate: function(dateString) {
        const date = new Date(dateString);
        const day = date.getUTCDate().toString().padStart(2, '0');
        const month = (date.getUTCMonth() + 1).toString().padStart(2, '0');
        const year = date.getUTCFullYear().toString().slice(-2);
        return `${day}/${month}/${year}`;
    },
    view: function() {
        return m("div.petition-list", [
            m("select.select.is-primary", { 
                    onchange: (e) => {
                        PetitionList.selection = e.target.value;
                        console.log(PetitionList.selection);  
                        PetitionList.loadPetitions(); // Recharger les pétitions après le changement de sélection
                    }
                }, [
                    m('option', { value: 'topHundred'}, 'Les plus signées'),
                    m('option', { value: 'myPet'}, 'Mes pétitions')
                ]),
            m("h2", { class: "title is-size-3" }, "Liste des pétitions créées"),
            PetitionList.petitions.length === 0 ? m("p", "Aucune pétition n'a été créée pour le moment.") : 
            m('div', [
                m('table', { class: 'table is-striped' }, [
                    m('tr', [
                        m('th', { width: "50px" }, "Action"),
                        m('th', { width: "100px" }, "Date"),
                        m('th', { width: "150px" }, "Titre"),
                        m('th', { width: "200px" }, "Auteur"),
                        m('th', { width: "250px" }, "Description")
                    ]),
                    PetitionList.petitions.map(function(petition) {
                        return m('tr', [
                            m('td', [
                                m('button.button.is-primary.btn-sign', 
                                    {
                                        onclick: function() {
                                            Petition.signPetition(petition.key.id);
                                        }
                                    }, "Signer",
                                ),
                                m('p', petition.propertyMap.nbSignatures + ' signature(s) enregistrée(s)'), 
                            ]),
                            m('td', m('label', PetitionList.formatDate(petition.propertyMap.date))),
                            m('td', m('label', petition.propertyMap.title)),
                            m('td', m('label', petition.propertyMap.autorName)),
                            m('td', m('label', petition.propertyMap.description))
                        ]);
                    })
                ])
            ])
        ]);
    }
};

// Charger les pétitions au démarrage
PetitionList.loadPetitions();