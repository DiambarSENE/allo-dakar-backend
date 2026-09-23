INSERT INTO roles (id, name, description, created_at, updated_at) VALUES
    (gen_random_uuid(), 'PASSENGER', 'Utilisateur pouvant rechercher et reserver des trajets', now(), now()),
    (gen_random_uuid(), 'DRIVER', 'Utilisateur pouvant publier des trajets', now(), now()),
    (gen_random_uuid(), 'ADMIN', 'Administrateur de la plateforme', now(), now()),
    (gen_random_uuid(), 'CUSTOMER_SERVICE', 'Support client', now(), now()),
    (gen_random_uuid(), 'MODERATOR', 'Moderation des avis et signalements', now(), now());
