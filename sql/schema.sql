-- =====================================================
-- Database: Booking Request API
-- PostgreSQL 15+
-- =====================================================


-- =====================================================
-- SUPPLIER TABLE
-- =====================================================

CREATE TABLE supplier (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    tax_id VARCHAR(50) NOT NULL UNIQUE,
    country VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    contact_email VARCHAR(150),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);



-- =====================================================
-- BOOKING REQUEST TABLE
-- =====================================================

CREATE TABLE booking_request (
    id BIGSERIAL PRIMARY KEY,
    booking_code VARCHAR(50) NOT NULL UNIQUE,
    issue_date DATE NOT NULL,
    expiration_date DATE NOT NULL,
    currency VARCHAR(10) NOT NULL,
    incoterm_code VARCHAR(20) NOT NULL,
    freight_mode VARCHAR(20) NOT NULL,
    origin_country VARCHAR(100) NOT NULL,
    destination_country VARCHAR(100) NOT NULL,
    fob_value NUMERIC(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    supplier_id BIGINT NOT NULL,

    CONSTRAINT fk_booking_supplier
        FOREIGN KEY (supplier_id)
        REFERENCES supplier(id),

    CONSTRAINT chk_booking_dates
        CHECK(issue_date <= expiration_date),

    CONSTRAINT chk_booking_currency
        CHECK(length(currency) >= 3),


    CONSTRAINT chk_incoterm_code
        CHECK(
            incoterm_code IN
            (
                'FOB',
                'CIF',
                'EXW',
                'DDP',
                'CFR'
            )
        ),


    CONSTRAINT chk_freight_mode
        CHECK(
            freight_mode IN
            (
                'AIR',
                'SEA',
                'ROAD'
            )
        ),


    CONSTRAINT chk_booking_status
        CHECK(
            status IN
            (
                'DRAFT',
                'CONFIRMED',
                'CANCELLED'
            )
        )

);



-- =====================================================
-- BOOKING ITEM TABLE
-- =====================================================

CREATE TABLE booking_item (

    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(15,2) NOT NULL,
    total_amount NUMERIC(15,2) NOT NULL,
    booking_request_id BIGINT NOT NULL,


    CONSTRAINT fk_item_booking
        FOREIGN KEY (booking_request_id)
        REFERENCES booking_request(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_quantity_positive
        CHECK(quantity > 0),

    CONSTRAINT chk_unit_price_positive
        CHECK(unit_price >= 0),

    CONSTRAINT chk_total_amount_positive
        CHECK(total_amount >= 0)

);

-- =====================================================
-- INDEXES
-- =====================================================

CREATE INDEX idx_supplier_tax_id
ON supplier(tax_id);

CREATE INDEX idx_booking_code
ON booking_request(booking_code);

CREATE INDEX idx_booking_status
ON booking_request(status);

CREATE INDEX idx_booking_issue_date
ON booking_request(issue_date);

CREATE INDEX idx_booking_supplier
ON booking_request(supplier_id);


