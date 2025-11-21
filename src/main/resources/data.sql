INSERT INTO candidate (id, registration_number, name, gender, pwd) VALUES
  (1, '101', 'Abhay', 'M', FALSE),
  (2, '102', 'Sneha', 'F', FALSE),
  (3, '103', 'Riya', 'F', TRUE),
  (4, '104', 'Prashant', 'M', TRUE);

INSERT INTO post (id, name) VALUES
  (1, 'Tech Assistant'),
  (2, 'Clerk'),
  (3, 'Data Entry Operator');

INSERT INTO candidate_application (id, candidate_id, post_id, status) VALUES
  (1, 1, 1, 'PENDING'),
  (2, 1, 2, 'PENDING'),
  (3, 2, 1, 'PENDING'),
  (4, 3, 1, 'PENDING'),
  (5, 4, 3, 'PENDING'),
  (6, 2, 2, 'PENDING'),
  (7, 3, 3, 'PENDING');

INSERT INTO center (id, center_name, capacity_per_slot, pwd_friendly) VALUES
  (1, 'MIT College Pune', 2, TRUE),
  (2, 'COEP Pune', 3, FALSE),
  (3, 'Government Engg College Pune', 1, TRUE);

INSERT INTO exam_slot (id, exam_date, slot_start_time, slot_end_time) VALUES
  (1, DATE '2025-09-15', TIME '09:00:00', TIME '10:30:00'),
  (2, DATE '2025-09-15', TIME '12:30:00', TIME '14:00:00'),
  (3, DATE '2025-09-15', TIME '16:00:00', TIME '17:30:00');

INSERT INTO center_slot_capacity (id, center_id, slot_id, capacity, used) VALUES
  (1, 1, 1, 2, 0),
  (2, 1, 2, 2, 0),
  (3, 1, 3, 2, 0),
  (4, 2, 1, 3, 0),
  (5, 2, 2, 3, 0),
  (6, 2, 3, 3, 0),
  (7, 3, 1, 1, 0),
  (8, 3, 2, 1, 0),
  (9, 3, 3, 1, 0);
