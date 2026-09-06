<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Convalida la tua richiesta - SoccorsoWeb</title>
    <link rel="stylesheet" type="text/css" href="${request.contextPath}/css/style.css">
</head>
<body class="flex-center-body">
    <main class="card-container">
        <header>
            <h1 class="section-title">Quasi fatto!</h1>
        </header>
        <section>
            <p>Abbiamo registrato i tuoi dati, ma per evitare falsi allarmi abbiamo bisogno di una tua conferma immediata.</p>
            <p class="text-muted">Clicca sul pulsante qui sotto per attivare la richiesta e inviare la squadra di soccorso.</p>
            
            <div class="mt-20 mb-24">
                <!-- ECCO LA MAGIA: Il bottone punta alla tua Servlet portandosi dietro il Token -->
                <a href="ConvalidaServlet?token=${token!}" class="btn btn-primary" style="padding: 15px 30px; font-size: 18px;">CONVALIDA RICHIESTA ORA</a>
            </div>
        </section>
        <hr>
        <footer>
            <p class="text-muted-small">Se non sei stato tu, ignora questa pagina e la richiesta si autodistruggerà tra 10 minuti.</p>
        </footer>
    </main>
</body>
</html>