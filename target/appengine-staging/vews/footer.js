let Footer = {
    view: function() {
        return m("footer.footer", [
            m("div.container", [
                m("p", [
                    "© 2024 Votre Site de Pétitions. Tous droits réservés. ",
                    m("a", { href: "#" }, "Politique de confidentialité"),
                    " | ",
                    m("a", { href: "#" }, "Conditions d'utilisation")
                ])
            ])
        ]);
    }
};