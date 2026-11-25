.PHONY: up down test package fmt validate plan apply destroy
up:
	docker compose up --build
down:
	docker compose down
test:
	mvn -B verify
package:
	mvn -B clean package
fmt:
	terraform -chdir=infra fmt -recursive
validate:
	terraform -chdir=infra init -backend=false
	terraform -chdir=infra validate
plan:
	terraform -chdir=infra plan
apply:
	terraform -chdir=infra apply
destroy:
	terraform -chdir=infra destroy
