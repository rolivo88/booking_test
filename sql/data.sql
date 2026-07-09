INSERT INTO supplier
(
    name,
    tax_id,
    country,
    address,
    contact_email
)
VALUES
(
    'Andes Import Solutions SPA',
    'RUT-99000111-1',
    'Chile',
    'Av. Las Condes 4500, Santiago',
    'contacto@andesimport.cl'
),
(
    'Pacific Ocean Trading Ltd',
    'TAX-88000222-2',
    'China',
    'Shanghai Free Trade Zone',
    'sales@pacifictrading.cn'
),
(
    'European Machines GmbH',
    'TAX-77000333-3',
    'Germany',
    'Hamburg Industrial Area 100',
    'export@eumachines.de'
)
ON CONFLICT (tax_id)
DO NOTHING;



-- =====================================================
-- BOOKING REQUESTS
-- =====================================================


-- DRAFT - Puede actualizarse y cambiar estado
INSERT INTO booking_request
(
    booking_code,
    issue_date,
    expiration_date,
    currency,
    incoterm_code,
    freight_mode,
    origin_country,
    destination_country,
    fob_value,
    status,
    supplier_id
)
VALUES
(
    'BOOK-TEST-0001',
    '2026-01-05',
    '2026-02-05',
    'USD',
    'FOB',
    'SEA',
    'China',
    'Chile',
    12500.00,
    'DRAFT',
    (
        SELECT id
        FROM supplier
        WHERE tax_id='TAX-88000222-2'
    )
);



-- CONFIRMED - Solo permite cancelar
INSERT INTO booking_request
(
    booking_code,
    issue_date,
    expiration_date,
    currency,
    incoterm_code,
    freight_mode,
    origin_country,
    destination_country,
    fob_value,
    status,
    supplier_id
)
VALUES
(
    'BOOK-TEST-0002',
    '2026-02-01',
    '2026-03-01',
    'EUR',
    'CIF',
    'SEA',
    'Germany',
    'Chile',
    45000.00,
    'CONFIRMED',
    (
        SELECT id
        FROM supplier
        WHERE tax_id='TAX-77000333-3'
    )
);



-- CANCELLED - Estado terminal
INSERT INTO booking_request
(
    booking_code,
    issue_date,
    expiration_date,
    currency,
    incoterm_code,
    freight_mode,
    origin_country,
    destination_country,
    fob_value,
    status,
    supplier_id
)
VALUES
(
    'BOOK-TEST-0003',
    '2026-03-10',
    '2026-04-10',
    'USD',
    'EXW',
    'AIR',
    'China',
    'Chile',
    9000.00,
    'CANCELLED',
    (
        SELECT id
        FROM supplier
        WHERE tax_id='TAX-88000222-2'
    )
);



-- DRAFT AIR - Para probar filtros
INSERT INTO booking_request
(
    booking_code,
    issue_date,
    expiration_date,
    currency,
    incoterm_code,
    freight_mode,
    origin_country,
    destination_country,
    fob_value,
    status,
    supplier_id
)
VALUES
(
    'BOOK-TEST-0004',
    '2026-04-15',
    '2026-05-15',
    'USD',
    'DDP',
    'AIR',
    'Chile',
    'Peru',
    32000.00,
    'DRAFT',
    (
        SELECT id
        FROM supplier
        WHERE tax_id='RUT-99000111-1'
    )
);



-- =====================================================
-- BOOKING ITEMS
-- =====================================================


-- Items Booking 1

INSERT INTO booking_item
(
    sku,
    description,
    quantity,
    unit_price,
    total_amount,
    booking_request_id
)
VALUES
(
    'SKU-NB-001',
    'Notebook empresarial',
    10,
    850.00,
    8500.00,
    (
        SELECT id
        FROM booking_request
        WHERE booking_code='BOOK-TEST-0001'
    )
),
(
    'SKU-MS-001',
    'Mouse wireless',
    50,
    25.00,
    1250.00,
    (
        SELECT id
        FROM booking_request
        WHERE booking_code='BOOK-TEST-0001'
    )
);



-- Items Booking 2

INSERT INTO booking_item
(
    sku,
    description,
    quantity,
    unit_price,
    total_amount,
    booking_request_id
)
VALUES
(
    'SKU-MTR-001',
    'Motor industrial',
    5,
    8000.00,
    40000.00,
    (
        SELECT id
        FROM booking_request
        WHERE booking_code='BOOK-TEST-0002'
    )
),
(
    'SKU-SEN-001',
    'Sensor electrónico',
    20,
    250.00,
    5000.00,
    (
        SELECT id
        FROM booking_request
        WHERE booking_code='BOOK-TEST-0002'
    )
);



-- Items Booking 3

INSERT INTO booking_item
(
    sku,
    description,
    quantity,
    unit_price,
    total_amount,
    booking_request_id
)
VALUES
(
    'SKU-PH-001',
    'Dispositivo móvil industrial',
    15,
    600.00,
    9000.00,
    (
        SELECT id
        FROM booking_request
        WHERE booking_code='BOOK-TEST-0003'
    )
);



-- Items Booking 4

INSERT INTO booking_item
(
    sku,
    description,
    quantity,
    unit_price,
    total_amount,
    booking_request_id
)
VALUES
(
    'SKU-CB-001',
    'Cable industrial',
    200,
    15.00,
    3000.00,
    (
        SELECT id
        FROM booking_request
        WHERE booking_code='BOOK-TEST-0004'
    )
),
(
    'SKU-BX-001',
    'Caja protección',
    100,
    30.00,
    3000.00,
    (
        SELECT id
        FROM booking_request
        WHERE booking_code='BOOK-TEST-0004'
    )
);