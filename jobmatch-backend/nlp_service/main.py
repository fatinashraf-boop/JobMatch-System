from fastapi import FastAPI
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer, util
from typing import List
import re


app = FastAPI(
    title="JobMatch NLP Service",
    version="1.0"
)


# =====================================================
# LOAD NLP MODEL
# =====================================================

model = SentenceTransformer(
    "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
)


# =====================================================
# REQUEST MODEL
# =====================================================

class MatchRequest(BaseModel):
    candidate_text: str
    job_text: str

    candidate_skills: List[str]
    required_skills: List[str]


# =====================================================
# HEALTH CHECK
# =====================================================

@app.get("/")
def health_check():

    return {
        "success": True,
        "message": "JobMatch NLP Service is running."
    }


# =====================================================
# NORMALIZE SKILL
# =====================================================

def normalize_skill(skill: str):

    skill = skill.lower().strip()

    # Replace repeated whitespace with one space
    skill = re.sub(r"\s+", " ", skill)

    return skill


# =====================================================
# CALCULATE SKILL COVERAGE
# =====================================================

def calculate_skill_coverage(
    candidate_skills: List[str],
    required_skills: List[str]
):

    # Normalize candidate skills
    candidate_set = {
        normalize_skill(skill)
        for skill in candidate_skills
        if skill.strip()
    }

    # Normalize required skills
    required_set = {
        normalize_skill(skill)
        for skill in required_skills
        if skill.strip()
    }

    # If job has no required skills
    if not required_set:

        return {
            "skill_score": 100.0,
            "matched_skills": [],
            "missing_skills": []
        }

    matched_skills = sorted(
        candidate_set.intersection(required_set)
    )

    missing_skills = sorted(
        required_set.difference(candidate_set)
    )

    skill_score = (
        len(matched_skills)
        / len(required_set)
    ) * 100

    return {
        "skill_score": round(skill_score, 2),
        "matched_skills": matched_skills,
        "missing_skills": missing_skills
    }


# =====================================================
# SEMANTIC + SKILL MATCH
# =====================================================

@app.post("/match")
def calculate_match(request: MatchRequest):

    candidate_text = request.candidate_text.strip()
    job_text = request.job_text.strip()

    if not candidate_text or not job_text:

        return {
            "success": False,
            "message":
                "Candidate text and job text are required."
        }

    # =================================================
    # SEMANTIC MATCHING
    # =================================================

    embeddings = model.encode(
        [
            candidate_text,
            job_text
        ],
        convert_to_tensor=True
    )

    candidate_embedding = embeddings[0]
    job_embedding = embeddings[1]

    similarity = util.cos_sim(
        candidate_embedding,
        job_embedding
    ).item()

    semantic_score = max(
        0.0,
        min(
            100.0,
            similarity * 100
        )
    )


    # =================================================
    # SKILL COVERAGE
    # =================================================

    skill_result = calculate_skill_coverage(
        request.candidate_skills,
        request.required_skills
    )

    # =================================================
    # HYBRID MATCH SCORE
    # =================================================

    skill_score = skill_result["skill_score"]

    final_score = (
        (semantic_score * 0.80)
        +
        (skill_score * 0.20)
    )

    final_score = round(
        max(0.0, min(100.0, final_score)),
        2
    )

    # =================================================
    # MATCH REASON
    # =================================================

    matched_skills = skill_result["matched_skills"]
    missing_skills = skill_result["missing_skills"]

    if final_score >= 80:
        strength = "Strong match"
    elif final_score >= 60:
        strength = "Good match"
    elif final_score >= 40:
        strength = "Moderate match"
    else:
        strength = "Low match"

    if matched_skills:
        matched_text = ", ".join(matched_skills)

        match_reason = (
            f"{strength}. Semantic compatibility is "
            f"{round(semantic_score, 2)}%. "
            f"Matched skills: {matched_text}."
        )
    else:
        match_reason = (
            f"{strength}. Semantic compatibility is "
            f"{round(semantic_score, 2)}%. "
            f"No explicit required skills were matched."
        )

    # =================================================
    # RESPONSE
    # =================================================

    return {
        "success": True,

        "semantic_score":
            round(semantic_score, 2),

        "skill_score":
            skill_score,

        "final_score":
            final_score,

        "matched_skills":
            matched_skills,

        "missing_skills":
            missing_skills,

        "match_reason":
            match_reason,

        "algorithm":
            "multilingual-sentence-transformer+skill-coverage-v2"
    }