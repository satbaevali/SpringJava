

INSERT INTO category (name, description) VALUES
    ('Электроника', 'Гаджеты, смартфоны и компьютеры'),
    ('Книги', 'Учебники и художественная литература');

INSERT INTO product (name, price, stock, category_id, version) VALUES
    ('Ноутбук Apple MacBook Air', 1199.99, 10, (SELECT id FROM category WHERE name = 'Электроника'), 0),
    ('Смартфон iPhone 15', 899.99, 15, (SELECT id FROM category WHERE name = 'Электроника'), 0),
    ('Беспроводные наушники Sony', 199.99, 25, (SELECT id FROM category WHERE name = 'Электроника'), 0),
    ('Изучаем Java (Head First)', 35.50, 50, (SELECT id FROM category WHERE name = 'Книги'), 0),
    ('Spring в действии (Craig Walls)', 45.00, 30, (SELECT id FROM category WHERE name = 'Книги'), 0);
