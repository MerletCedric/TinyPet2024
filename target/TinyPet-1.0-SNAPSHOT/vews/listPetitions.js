let PetitionList = {
    petitions: [],
    loadPetitions: function() {
        m.request({
            method: "GET",
            url: "/_ah/api/myApi/v1/getPetitions",
            withCredentials: true,
        })
        .then(function(result) {
            PetitionList.petitions = result.items;
            m.redraw();
        });
    },
    view: function() {
        return m("div.petition-list", [
            m("h2", { class: "title is-size-3" }, "Liste des pétitions créées"),
            PetitionList.petitions.length === 0 ? m("p", "Aucune pétition n'a été créée pour le moment.") : 
            m('div', [
                m('table', { class: 'table is-striped' }, [
                    m('tr', [
                        m('th', { width: "50px" }, "Action"),
                        m('th', { width: "150px" }, "Titre"),
                        m('th', { width: "100px" }, "Auteur"),
                        m('th', { width: "200px" }, "Description")
                    ]),
                    PetitionList.petitions.map(function(petition) {
                        return m('tr', [
                            m('td', m('button.button.is-primary.btn-sign', "Signer")),
                            m('td', m('label', petition.properties.title)),
                            m('td', m('label', petition.properties.autor)),
                            m('td', m('label', petition.properties.description))
                        ]);
                    })
                ])
            ])
        ]);
    }
};

// Charger les pétitions au démarrage
PetitionList.loadPetitions();
