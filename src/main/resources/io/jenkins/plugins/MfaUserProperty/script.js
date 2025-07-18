function initMfaToggle() {
    const checkbox = document.getElementById('mfaEnabledCheckbox');
    const container = document.getElementById('mfaSetupContainer');
    const qrImage = document.getElementById('qrCodeImage');
    const secretInput = document.querySelector('input[name="_.secretKey"]');

    function showOrHideMfaSetup() {
        if (checkbox.checked) {
            container.style.display = 'block';

            if (!qrImage.src) {
                fetch(window.rootURL + '/mfa-totp/generateSecret')
                    .then(resp => resp.json())
                    .then(data => {
                        qrImage.src = window.rootURL + '/mfa-totp/qrcode?secret=' + 
                                      encodeURIComponent(data.secret);
                        secretInput.value = data.secret;
                    })
                    .catch(err => {
                        console.error('Error generating QR Code:', err);
                    });
            }
        } else {
            container.style.display = 'none';
            qrImage.src = '';
            secretInput.value = '';
        }
    }

    checkbox.addEventListener('change', showOrHideMfaSetup);

    if (checkbox.checked) {
        showOrHideMfaSetup();
    }
}

// Exporta a função para ser chamada pelo jelly
window.initMfaToggle = initMfaToggle;