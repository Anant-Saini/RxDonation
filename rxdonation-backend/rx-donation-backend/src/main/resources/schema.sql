-- 1. Ensure PostGIS extension is active in this database
CREATE EXTENSION IF NOT EXISTS postgis;

-- 2. Create the User Roles Enum
CREATE TYPE user_role_enum AS ENUM ('DONOR', 'PHARMACY');

-- 3. Create the Core Credentials / Auth Table
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL, -- Will store BCrypt hashes
                       role user_role_enum NOT NULL,
                       is_verified BOOLEAN DEFAULT FALSE,
                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. Create the Donor Profile Table
CREATE TABLE donors (
                        id BIGSERIAL PRIMARY KEY,
                        user_id BIGINT UNIQUE NOT NULL,
                        name VARCHAR(255) NOT NULL,
                        address TEXT NOT NULL,
                        location GEOMETRY(Point, 4326) NOT NULL, -- PostGIS Spatial Point (WGS 84)

    -- Maintain strict 1:1 integrity with On Delete Cascade
                        CONSTRAINT fk_donor_user FOREIGN KEY (user_id)
                            REFERENCES users(id) ON DELETE CASCADE
);

-- 5. Create the Pharmacy Profile Table
CREATE TABLE pharmacies (
                            id BIGSERIAL PRIMARY KEY,
                            user_id BIGINT UNIQUE NOT NULL,
                            pharmacy_name VARCHAR(255) NOT NULL,
                            telephone VARCHAR(20) NOT NULL,
                            address TEXT NOT NULL,
                            opening_time TIME,
                            closing_time TIME,
                            location GEOMETRY(Point, 4326) NOT NULL, -- PostGIS Spatial Point (WGS 84)

    -- Maintain strict 1:1 integrity with On Delete Cascade
                            CONSTRAINT fk_pharmacy_user FOREIGN KEY (user_id)
                                REFERENCES users(id) ON DELETE CASCADE
);

-- 6. Infrastructure Optimization: Create Spatial Indexes (GIST)
-- This is what gives your 2km radius query O(log N) performance instead of scanning every row!
CREATE INDEX idx_donors_location ON donors USING gist(location);
CREATE INDEX idx_pharmacies_location ON pharmacies USING gist(location);