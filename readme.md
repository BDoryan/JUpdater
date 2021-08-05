# JUpdater
Ce projet est une bibliothèque qui a pour but de faire des mises à jour automatique pour des logiciels. Ce projet n'a pas pour but de remplacer la grande bibliothèque **Update4J** mais à juste pour but de simplifier la mise en place de mise à jour.

## *Client*
Le client se connecte à un serveur de mise à jour, celui-ci s'identifie avec la version du logiciel. Si le serveur trouve que la version n'est pas à jour le client envoie son "manifest" ce qui permet au serveur d'identifier les fichiers à envoyer pour la mise à jour

## *Serveur de mise à jour*
Le serveur attend que le client se connecte, une fois qu'un client se connecte on compare sa version avec celle du serveur (la dernière version de votre logiciel). Si le serveur estime que le client n'est pas à jour il va avertir le client et le serveur va attendre le "manifeste" du client. Quand le serveur a reçu le manifeste il va comparer les fichiers qui doivent mettre à jour puis les envoies au client
