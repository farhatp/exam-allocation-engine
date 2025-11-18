INSERT INTO candidate (id, registration_number, name, gender, pwd) VALUES
  (1, '101', 'Abhay', 'M', FALSE),
  (2, '102', 'Sneha', 'F', FALSE),
  (3, '103', 'Riya', 'F', TRUE);

INSERT INTO post (id, name) VALUES
  (1, 'Tech Assistant'),
  (2, 'Clerk');

INSERT INTO candidate_application (id, candidate_id, post_id, status) VALUES
  (1, 1, 1, 'PENDING'),
  (2, 1, 2, 'PENDING'),
  (3, 2, 1, 'PENDING'),
  (4, 3, 1, 'PENDING');

INSERT INTO center (id, center_name, capacity_per_slot, pwd_friendly) VALUES
  (1, 'MIT College Pune', 2, TRUE),
  (2, 'COEP Pune', 3, FALSE);

INSERT INTO exam_slot (id, exam_date, slot_time) VALUES
  (1, DATE '2025-09-15', TIME '09:00:00'),
  (2, DATE '2025-09-15', TIME '11:00:00'),
  (3, DATE '2025-09-15', TIME '16:00:00');

INSERT INTO center_slot_capacity (id, center_id, slot_id, capacity, used) VALUES
  (1, 1, 1, 2, 0),
  (2, 1, 2, 2, 0),
  (3, 1, 3, 2, 0),
  (4, 2, 1, 3, 0),
  (5, 2, 2, 3, 0),
  (6, 2, 3, 3, 0);
