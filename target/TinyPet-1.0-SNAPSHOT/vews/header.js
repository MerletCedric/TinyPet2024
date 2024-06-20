function  handleCredentialResponse(response) {
    // decodeJwtResponse() 
    const responsePayload = jwt_decode(response.credential);

    Petition.userId = responsePayload.sub;
    Petition.authentificate=true;    
    Petition.name=responsePayload.name;
    Petition.userToken = response.credential;

    const userInfo = {
        userId: responsePayload.sub,
        authentificate: true,
        name: responsePayload.name
    };

    Petition.autorName = userInfo.name;
    Petition.autorId = userInfo.userId;

    // Convertir l'objet en chaîne JSON et le stocker dans le localStorage
    localStorage.setItem('user', JSON.stringify(userInfo));
    
    m.route.set("/secret")
}

let Banner = {
    view: function() {
        return m("div.banner", { style: { backgroundColor: '#3e3e3e' } }, [
            m("div.banner-content", [
                m("h1", { class: "title is-size-3", style: { color: 'white' } }, "Créez une pétition"),
                m("p", { class: "subtitle is-size-5", style: { color: 'white' } }, "Rejoignez-nous pour faire entendre votre voix!")
            ]),
            m("div", {
                "id":"g_id_onload",
                "data-client_id":"113524796838-sn3sk232hsqa0p04r1opnpbb1htkbqrj.apps.googleusercontent.com",
                "data-callback":"handleCredentialResponse"}),
            m("div", {
                "class":"g_id_signin",
                "data-type":"standard"})
        ])
    }
};