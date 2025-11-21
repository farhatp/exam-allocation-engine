# exam-allocation-engine
Exam Slot and Center Allocation Engine for allocating the seat to the candidate applications

### 1. Overview 
The Exam Seat Allocation Engine is developed using Spring Boot (Java 17), H2 in-memory database, Spring Security with Basic Authentication, Jakarta for entity and validation support, and Lombok to reduce boilerplate. The API server port is set to public in GitHub Code Space to allow testing via curl and Postman. The application uses Asia/Kolkata time zone. 

### 2. Technologies 
- Spring Boot, Spring Web, H2 DB  
- Spring Security (Basic Auth)   
- Jakarta Persistence (Entity mappings)   
- Lombok (Data, Builder annotations)   
- Time Zone: Asia/Kolkata 

### 3. API Endpoints 
- POST /api/allocate — runs the allocation engine (secured with Basic Auth)  
- GET /api/allocation/{registration_number} — fetch the candidate applications alongwith its allocation status whether allocated or pending 

### 4. Allocation Rules 
- 3 slots: 09:00–10:30, 12:30–14:00, 16:00–17:30   
- Female candidates not in last slot  
- PWD only in PWD-friendly centers   
- Multiple posts: same center, different slot, same date   
- Capacity per center/slot must not exceed   
- If rule conflict → status marked as PENDING   

### 5. Security Configuration 
- Basic Auth enabled:    
  - spring.security.user.name=admin   
  - spring.security.user.password=admin123   
- CSRF disabled  
- H2 console enabled and allowed   

### 6. Time Zone Configuration 
- @PostConstruct sets Asia/Kolkata time zone   
- spring.jackson.time-zone=Asia/Kolkata   
- spring.jpa.properties.hibernate.jdbc.time_zone=Asia/Kolkata   

### 7. Testing 
- curl:    
curl -X POST -u admin:admin123 https://fantastic-halibut-vgjjpqrrxg53xpr5-8080.app.github.dev/api/allocate  
- Postman: Use Basic Auth and POST   
- GET    
https://fantastic-halibut-vgjjpqrrxg53xpr5-8080.app.github.dev/api/allocation/102  
- H2 Database:   
https://fantastic-halibut-vgjjpqrrxg53xpr5-8080.app.github.dev/h2-console/ 

### 8. Summary 
A rule-based exam allocation engine with secure API, IST time configuration, slot logic, database integration, and remote testing support.
