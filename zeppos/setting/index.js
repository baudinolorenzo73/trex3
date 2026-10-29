AppSettingsPage({
  build(){
    return Section({},
      View({style:{padding:'12px'}},[
        Text({style:{fontSize:'20px',fontWeight:'bold'}},'T-Rex GPS Bridge 0.1.0'),
        Text({},'Apri T-Rex GPS Bridge sul tablet e copia il Codice collegamento.'),
        TextInput({label:'Codice collegamento',settingsKey:'bridge_token',placeholder:'32 caratteri dal tablet'}),
        Text({},'Avvia il ponte sul tablet. Mantieni Zepp attivo durante la prima prova.')
      ])
    );
  }
});
