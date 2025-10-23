-- noinspection SqlNoDataSourceInspectionForFile

CREATE TABLE MENU (ID VARCHAR(255) NOT NULL PRIMARY KEY,
                   NAME VARCHAR(255) UNIQUE NOT NULL,
                   DESCRIPTION VARCHAR(255),
                   CATEGORY VARCHAR(255),
                   PRICE FLOAT,
                   TAGS VARCHAR(255),
                   ADDONS VARCHAR(255));

CREATE TABLE ORDERS (ID VARCHAR(255) NOT NULL PRIMARY KEY,
                     NAME VARCHAR(255) UNIQUE NOT NULL,
                     CONTENT VARCHAR(255),
                     PRICE FLOAT);

INSERT INTO MENU (ID, NAME, DESCRIPTION, CATEGORY, PRICE, TAGS, ADDONS) VALUES ('1',
                                                                                'Latte',
                                                                                'A rich espresso drink with steamed milk.',
                                                                                'Drink',
                                                                                4.5,
                                                                                '["Hot", "Customizable", "Classic"]',
                                                                                '["Oat milk", "Soy milk", "Extra shot", "Caramel syrup"]');
INSERT INTO MENU (ID, NAME, DESCRIPTION, CATEGORY, PRICE, TAGS, ADDONS) VALUES ('2',
                                                                                'Cappuccino',
                                                                                'Espresso topped with a thick layer of frothy milk.',
                                                                                'Drink',
                                                                                4.0,
                                                                                '["Hot", "Foamy", "Classic"]',
                                                                                '["Vanilla syrup", "Extra shot"]');
INSERT INTO MENU (ID, NAME, DESCRIPTION, CATEGORY, PRICE, TAGS, ADDONS) VALUES ('3',
                                                                                'Espresso',
                                                                                'A strong shot of coffee for a quick energy boost.',
                                                                                'Drink',
                                                                                3.0,
                                                                                '["Hot", "Classic", "Bold"]',
                                                                                '["Extra shot"]');
INSERT INTO MENU (ID, NAME, DESCRIPTION, CATEGORY, PRICE, TAGS, ADDONS) VALUES ('4',
                                                                                'Iced Matcha Latte',
                                                                                'Chilled matcha green tea with milk.',
                                                                                'Drink',
                                                                                5.0,
                                                                                '["Cold", "Vegan", "Trendy" ]',
                                                                                '["Oat milk", "Almond milk", "Extra matcha"]');
INSERT INTO MENU (ID, NAME, DESCRIPTION, CATEGORY, PRICE, TAGS, ADDONS) VALUES ('5',
                                                                                'Avocado Toast',
                                                                                'Toasted bread topped with mashed avocado and seasonings.',
                                                                                'Food',
                                                                                6.5,
                                                                                '["Healthy", "Vegan", "Breakfast"]',
                                                                                '["Egg", "Feta cheese", "Hot sauce"]');
INSERT INTO MENU (ID, NAME, DESCRIPTION, CATEGORY, PRICE, TAGS, ADDONS) VALUES ('6',
                                                                                'Blueberry Muffin',
                                                                                'A soft muffin bursting with blueberries.',
                                                                                'Food',
                                                                                2.75,
                                                                                '["Breakfast", "Sweet", "Vegetarian"]',
                                                                                '["Warm up"]');
INSERT INTO MENU (ID, NAME, DESCRIPTION, CATEGORY, PRICE, TAGS, ADDONS) VALUES ('7',
                                                                                'Bagel with Cream Cheese',
                                                                                'A freshly baked bagel served with creamy cheese spread.',
                                                                                'Food',
                                                                                3.5,
                                                                                '["Breakfast", "Savory", "Vegetarian"]',
                                                                                '["Butter", "Jam"]');
INSERT INTO MENU (ID, NAME, DESCRIPTION, CATEGORY, PRICE, TAGS, ADDONS) VALUES ('8',
                                                                                'Caramel Frappuccino',
                                                                                'A blended coffee drink with caramel drizzle and whipped cream.',
                                                                                'Drink',
                                                                                5.5,
                                                                                '["Cold", "Sweet", "Indulgent"]',
                                                                                '["Extra caramel", "Whipped cream"]');
INSERT INTO MENU (ID, NAME, DESCRIPTION, CATEGORY, PRICE, TAGS, ADDONS) VALUES ('9',
                                                                                'Chocolate Chip Cookie',
                                                                                'A large, soft-baked cookie loaded with chocolate chips.',
                                                                                'Food',
                                                                                2.0,
                                                                                '["Snack", "Sweet", "Vegetarian"]',
                                                                                '["Warm up"]');
INSERT INTO MENU (ID, NAME, DESCRIPTION, CATEGORY, PRICE, TAGS, ADDONS) VALUES ('10',
                                                                                'Hot Chocolate',
                                                                                'A creamy hot chocolate drink topped with whipped cream.',
                                                                                'Drink',
                                                                                3.75,
                                                                                '["Hot", "Sweet", "Comforting"]',
                                                                                '["Extra whipped cream", "Caramel drizzle"]');

INSERT INTO ORDERS (ID, NAME, CONTENT, PRICE) VALUES ('1', 'Joe', 'Espresso', 3.0);
