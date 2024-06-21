let PetitionList = {
    petitions: [],
    loadPetitions: function() {
        m.request({
            method: "GET",
            url: "/getPetitions",
            withCredentials: true,
        })
        .then(function(result) {
            if (result && result.length > 0) {
                PetitionList.petitions = result;
            } else {
                PetitionList.petitions = [];
            }
            console.log(result);
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
                                m('button.button.is-primary.btn-sign', "Signer"),
                                m('p', 'Signé'+ petition.propertyMap.nbSignatures + ' fois.'),
                            ]),
                            m('td', m('label', PetitionList.formatDate(petition.propertyMap.date))),
                            m('td', m('label', petition.propertyMap.title)),
                            m('td', m('label', petition.propertyMap.autor)),
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
