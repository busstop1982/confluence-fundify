async function updatePage() {
    const sk = document.getElementById("risid").textContent;
    const result = document.getElementById("result");
    const endfield = document.getElementById("endfield"); 
    var endpoint = "/rest/scriptrunner/latest/custom/updateFundifyPage";
    const payload = { risID: `${sk}`}
    if ( confirm("This will overwrite any manual changes! Proceed?") ){
        fetch(endpoint,{
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payload)
        })
        .then(response => {
            if (!response.ok) throw new Error('Failed to send');
            return response.json();
        })
        .then(data => {
            result.innerHTML = JSON.stringify(data,null, 2)
            //location.reload()
        })
        .catch(error => {
            result.innerHTML = `Error: ${error.message}`
        });
    }
}