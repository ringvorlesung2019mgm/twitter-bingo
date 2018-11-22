from OpenSSL import crypto
import os.path

"""
Converts a x509 certificate to a PEM-encoded string
"""
def dump_cert(cert):
    out = ""
    out += "Subject: "+ ", ".join(x[0].decode()+":"+x[1].decode() for x in cert.get_subject().get_components())+"\n"
    out += "Issuer: "+ ", ".join(x[0].decode()+":"+x[1].decode() for x in cert.get_issuer().get_components())+"\n"
    out += crypto.dump_certificate(crypto.FILETYPE_PEM,cert).decode()
    return out

"""
Given a file-path leading to a pkcs12 certificate-store this function converts the file to the PEM format and stores it with the same name but .pem suffix
"""
def pkcs12_to_pem(file,password,skip_existing=False):
    filename, _ = os.path.splitext(file)
    newfile = filename + ".pem"
    if os.path.exists(newfile) and skip_existing:
        return
        
    with open(file,"rb") as f:
        pkcs = crypto.load_pkcs12(f.read(),password)
        cert = ""
        pkey = ""
        cas = []
        if pkcs.get_certificate():
            cert = dump_cert(pkcs.get_certificate())
        if pkcs.get_privatekey():
            pkey = crypto.dump_privatekey(crypto.FILETYPE_PEM,pkcs.get_privatekey(),'aes256',password.encode()).decode()
        for ca in pkcs.get_ca_certificates():
            cas.append(dump_cert(ca))
        filecontent = cert+"\n"+pkey+"\n".join(cas)
        with open(newfile,"w") as t:
            t.write(filecontent)